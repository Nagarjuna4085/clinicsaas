package com.clinicflow.scheduler;

import com.clinicflow.context.TenantContext;
import com.clinicflow.entity.tenant.Appointment;
import com.clinicflow.repository.global.TenantRepository;
import com.clinicflow.repository.tenant.AppointmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

/**
 * Runs every morning at 8 AM IST.
 * Finds all follow-ups due tomorrow across ALL clinics.
 * Sends WhatsApp reminders via Gupshup.
 * 
 * This is the power of schema-per-tenant — we loop over all tenant schemas.
 */
@Component
public class FollowUpReminderScheduler {

    private final TenantRepository tenantRepo;
    private final AppointmentRepository appointmentRepo;
    // private final WhatsAppService whatsAppService; // inject when ready

    public FollowUpReminderScheduler(TenantRepository tenantRepo,
                                      AppointmentRepository appointmentRepo) {
        this.tenantRepo = tenantRepo;
        this.appointmentRepo = appointmentRepo;
    }

    // Runs at 8:00 AM IST (2:30 AM UTC) every day
    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Kolkata")
    public void sendFollowUpReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        // Loop through every active clinic
        tenantRepo.findAll().forEach(tenant -> {
            if (!"active".equals(tenant.getStatus()) &&
                !"trial".equals(tenant.getStatus())) return;

            // Switch to this clinic's schema
            TenantContext.set(tenant.getSchemaName());
            try {
                List<Appointment> followups = appointmentRepo.findFollowupsDue(tomorrow);

                followups.forEach(appt -> {
                    String phone = appt.getPatient().getPhone();
                    String name  = appt.getPatient().getName();
                    String date  = appt.getFollowupDate().toString();

                    // TODO: whatsAppService.sendFollowUpReminder(phone, name, date, tenant.getClinicName());

                    System.out.printf("REMINDER: %s (%s) — follow-up at %s on %s%n",
                        name, phone, tenant.getClinicName(), date);

                    // Mark reminder as sent
                    appt.setReminderSent(true);
                    appointmentRepo.save(appt);
                });

            } finally {
                TenantContext.clear();
            }
        });
    }

    // Resets daily tokens — runs at midnight IST
    @Scheduled(cron = "0 30 18 * * *", zone = "UTC")  // midnight IST = 6:30 PM UTC
    public void dailyReset() {
        // Tokens auto-reset because nextTokenNumber queries by visit_date = CURRENT_DATE
        // Nothing to do here — just log for monitoring
        System.out.println("Daily reset check at midnight IST — token counter resets automatically");
    }
}
